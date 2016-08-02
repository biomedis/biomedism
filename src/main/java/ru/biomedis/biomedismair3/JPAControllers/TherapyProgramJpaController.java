/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import java.io.Serializable;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import javax.persistence.EntityNotFoundException;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.Program;
import ru.biomedis.biomedismair3.entity.TherapyProgram;

/**
 *
 * @author Anama
 */
public class TherapyProgramJpaController implements Serializable {

    public TherapyProgramJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TherapyProgram therapyProgram) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(therapyProgram);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TherapyProgram therapyProgram) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            therapyProgram = em.merge(therapyProgram);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = therapyProgram.getId();
                if (findTherapyProgram(id) == null) {
                    throw new NonexistentEntityException("The therapyProgram with id " + id + " no longer exists.");
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
            TherapyProgram therapyProgram;
            try {
                therapyProgram = em.getReference(TherapyProgram.class, id);
                therapyProgram.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The therapyProgram with id " + id + " no longer exists.", enfe);
            }
            em.remove(therapyProgram);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TherapyProgram> findTherapyProgramEntities() {
        return findTherapyProgramEntities(true, -1, -1);
    }

    public List<TherapyProgram> findTherapyProgramEntities(int maxResults, int firstResult) {
        return findTherapyProgramEntities(false, maxResults, firstResult);
    }

    private List<TherapyProgram> findTherapyProgramEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TherapyProgram.class));
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

    public TherapyProgram findTherapyProgram(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TherapyProgram.class, id);
        } finally {
            em.close();
        }
    }

    public int getTherapyProgramCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TherapyProgram> rt = cq.from(TherapyProgram.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }


}
