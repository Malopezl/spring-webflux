package gt.com.archteam.springboot.reactor.app;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import gt.com.archteam.springboot.reactor.app.models.Comentarios;
import gt.com.archteam.springboot.reactor.app.models.Usuario;
import gt.com.archteam.springboot.reactor.app.models.UsuarioComentarios;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootApplication
public class SpringBootReactorApplication implements CommandLineRunner {
	private static final Logger log = LoggerFactory.getLogger(SpringBootReactorApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SpringBootReactorApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		ejemploInfiniteInterval();
	}

	public void ejemploInfiniteInterval() throws InterruptedException {
		var latch = new CountDownLatch(1);

		Flux.interval(Duration.ofSeconds(1))
				.doOnTerminate(latch::countDown)
				.flatMap(i -> {
					if (i >= 5) {
						return Flux.error(new InterruptedException("Solo hasta 5"));
					}
					return Flux.just(i);
				})
				.map(i -> "Hola " + i)
				.retry(2)
				.subscribe(log::info, e -> log.error(e.getMessage()));

		latch.await();
	}

	public void ejemploDelayElements() {
		var rango = Flux.range(1, 12)
				.delayElements(Duration.ofSeconds(1))
				.doOnNext(i -> log.info(i.toString()));

		rango.subscribe();
		// rango.blockLast();
	}

	public void ejemploInterval() {
		var rango = Flux.range(1, 12);
		var retraso = Flux.interval(Duration.ofSeconds(1));

		/*
		 * Aunque la ejecucion termina, el proceso se sigue ejecutando en segundo plano
		 */
		rango.zipWith(retraso, (range, delay) -> range)
				.doOnNext(i -> log.info(i.toString()))
				// .subscribe();
				/*
				 * Se utiliza esta funcion solo para poder visualizar el proceso pero no se
				 * recomienda porque puede generar cuellos de botella.
				 */
				.blockLast();
	}

	public void ejemploZipWithRangos() {
		Flux.just(1, 2, 3, 4)
				.map(i -> (i * 2))
				.zipWith(Flux.range(0, 4), (uno, dos) -> String.format("Primer Flux: %d, Segundo Flux: %d", uno, dos))
				.subscribe(log::info);
	}

	public void ejemploUsuarioComentariosZipWithForma2() {
		var usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));
		var comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo!");
			comentarios.addComentario("Adios mundo!");
			comentarios.addComentario("Comentarios pruebas");
			return comentarios;
		});

		/* Esta forma genera una tupla con los tipos de datos y se obtienen de esta forma */
		var usuarioComentarios = usuarioMono.zipWith(comentariosUsuarioMono)
		.map(tuple -> {
			var usuario = tuple.getT1();
			var comentarios = tuple.getT2();
			return new UsuarioComentarios(usuario, comentarios);
		});
		usuarioComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosZipWith() {
		var usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));
		var comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo!");
			comentarios.addComentario("Adios mundo!");
			comentarios.addComentario("Comentarios pruebas");
			return comentarios;
		});

		var usuarioComentarios = usuarioMono.zipWith(comentariosUsuarioMono, UsuarioComentarios::new);
		usuarioComentarios.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploUsuarioComentariosFlatMap() {
		var usuarioMono = Mono.fromCallable(() -> new Usuario("John", "Doe"));
		var comentariosUsuarioMono = Mono.fromCallable(() -> {
			Comentarios comentarios = new Comentarios();
			comentarios.addComentario("Hola mundo!");
			comentarios.addComentario("Adios mundo!");
			comentarios.addComentario("Comentarios pruebas");
			return comentarios;
		});

		usuarioMono.flatMap(u -> comentariosUsuarioMono.map(c -> new UsuarioComentarios(u, c)))
		.subscribe(uc -> log.info(uc.toString()));
	}

	public void ejemploCollectList() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Rodriguez"));
		usuariosList.add(new Usuario("Diego", "Rodriguez"));
		usuariosList.add(new Usuario("Juan", "Lopez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.collectList()
				.subscribe(lista -> lista.forEach(item -> log.info(item.toString())));
	}

	public void ejemploToString() {
		List<Usuario> usuariosList = new ArrayList<>();
		usuariosList.add(new Usuario("Andres", "Guzman"));
		usuariosList.add(new Usuario("Pedro", "Rodriguez"));
		usuariosList.add(new Usuario("Diego", "Rodriguez"));
		usuariosList.add(new Usuario("Juan", "Lopez"));
		usuariosList.add(new Usuario("Bruce", "Lee"));
		usuariosList.add(new Usuario("Bruce", "Willis"));

		Flux.fromIterable(usuariosList)
				.map(usuario -> usuario.getNombre().toUpperCase().concat(" ")
						.concat(usuario.getApellido().toUpperCase()))
				.flatMap(nombre -> {
					if (nombre.contains("bruce".toUpperCase())) {
						return Mono.just(nombre);
					}
					return Mono.empty();
				})
				.map(String::toLowerCase)
				.subscribe(log::info);
	}

	public void ejemploFlatMap() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Velasquez");
		usuariosList.add("Diego Rodriguez");
		usuariosList.add("Juan Lopez");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		Flux.fromIterable(usuariosList)
				.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.flatMap(usuario -> {
					if (usuario.getNombre().equalsIgnoreCase("bruce")) {
						return Mono.just(usuario);
					}
					return Mono.empty();
				})
				.map(usuario -> usuario.getNombre().toLowerCase())
				.subscribe(log::info);
	}

	public void ejemploIterable() {
		List<String> usuariosList = new ArrayList<>();
		usuariosList.add("Andres Guzman");
		usuariosList.add("Pedro Velasquez");
		usuariosList.add("Diego Rodriguez");
		usuariosList.add("Juan Lopez");
		usuariosList.add("Bruce Lee");
		usuariosList.add("Bruce Willis");

		var nombres = Flux.fromIterable(usuariosList); /* Flux.just("Andres Guzman", "Pedro Velasquez", "Diego Rodriguez", "Juan Lopez", "Bruce Lee", "Bruce Willis"); */

		var usuarios = nombres.map(nombre -> new Usuario(nombre.split(" ")[0].toUpperCase(), nombre.split(" ")[1].toUpperCase()))
				.filter(usuario -> usuario.getNombre().equalsIgnoreCase("bruce"))
				.doOnNext(usuario -> {
					if (usuario == null) {
						throw new RuntimeException("Nombres no pueden ser vacios");
					}
					System.out.println(usuario.getNombre().concat(" ").concat(usuario.getApellido()));
				})
				.map(usuario -> usuario.getNombre().toLowerCase());
		/* Si no se subscribe al objeto no va a mostrar nada... */
		usuarios.subscribe(log::info, error -> log.error(error.getMessage()), new Runnable() {

			@Override
			public void run() {
				log.info("Ha finalizado la ejecucion del observable con exito!");
			}

		});
	}

}
