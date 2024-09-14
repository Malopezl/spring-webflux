package gt.com.archteam.springboot.reactor.app;

import java.util.ArrayList;
import java.util.List;

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
		ejemploUsuarioComentariosFlatMap();
	}

	public void ejemploUsuarioComentariosFlatMap() throws Exception {
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

	public void ejemploCollectList() throws Exception {
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

	public void ejemploToString() throws Exception {
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

	public void ejemploFlatMap() throws Exception {
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

	public void ejemploIterable() throws Exception {
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
