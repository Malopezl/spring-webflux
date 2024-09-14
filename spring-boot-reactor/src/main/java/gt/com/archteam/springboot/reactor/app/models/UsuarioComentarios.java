package gt.com.archteam.springboot.reactor.app.models;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class UsuarioComentarios {
    private Usuario usuario;
    private Comentarios comentarios;
    
    @Override
    public String toString() {
        return "UsuarioComentarios [usuario=" + usuario + ", comentarios=" + comentarios + "]";
    }
}
