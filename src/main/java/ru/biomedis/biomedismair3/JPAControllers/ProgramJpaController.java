/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.Section;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Anama
 */
public class ProgramJpaController implements Serializable {

    public ProgramJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Program program)  throws Exception{
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(program);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Program program) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            program = em.merge(program);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = program.getId();
                if (findProgram(id) == null) {
                    throw new NonexistentEntityException("The program with id " + id + " no longer exists.");
                }
            }
            throw ex;
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void destroy(Long id) throws NonexistentEntityException {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Program program;
            try {
                program = em.getReference(Program.class, id);
                program.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The program with id " + id + " no longer exists.", enfe);
            }
            em.remove(program);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Program> findProgramEntities() {
        return findProgramEntities(true, -1, -1);
    }

    public List<Program> findProgramEntities(int maxResults, int firstResult) {
        return findProgramEntities(false, maxResults, firstResult);
    }

    private List<Program> findProgramEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Program.class));
            Query q = em.createQuery(cq);
            if (!all) {
                q.setMaxResults(maxResults);
                q.setFirstResult(firstResult);
            }
            return q.getResultList();
        } finally {
            em.close();
        }
    }

    public Program findProgram(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Program.class, id);
        } finally {
            em.close();
        }
    }

    public int getProgramCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Program> rt = cq.from(Program.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }

   

    public List<Program> findAllProgramByComplex(Complex complex)
    {
         EntityManager em = getEntityManager();
         Query query = em.createQuery("SELECT p FROM Program p WHERE p.complex=:complex");
         query.setParameter("complex", complex);
         return query.getResultList();
    }
    
     public List<Program> findAllProgramBySection(Section section)
    {
          EntityManager em = getEntityManager();
          Query query = em.createQuery("SELECT p FROM Program p WHERE p.section=:section");
          query.setParameter("section", section);
          return query.getResultList();
    }
}
