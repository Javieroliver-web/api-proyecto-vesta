import dao.UsuarioDAO;
import entity.Usuario;

public class Inicio {
    public static void main(String[] args) {
        UsuarioDAO dao = new UsuarioDAO();

        Usuario nuevo = new Usuario();
        nuevo.setNombre("Laura");
        nuevo.setEdad(5);

        dao.insertar(nuevo);
    }
}