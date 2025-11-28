package dao;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import entity.Usuario;

public class UsuarioDAO {
      private EntityManagerFactory emf = Persistence.createEntityManagerFactory("alumnosPU");

      public void insertar(Usuario alumno) {
        EntityManager em = emf.createEntityManager();
        em.getTransaction().begin();
        em.persist(alumno);
        em.getTransaction().commit();
        em.close();
      }
    }