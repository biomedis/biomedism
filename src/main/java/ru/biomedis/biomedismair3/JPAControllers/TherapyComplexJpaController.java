/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.Profile;

import ru.biomedis.biomedismair3.entity.TherapyComplex;

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
public class TherapyComplexJpaController implements Serializable {

    public TherapyComplexJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(TherapyComplex therapyComplex) {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Profile profile = therapyComplex.getProfile();
            if (profile != null) {
                profile = em.getReference(profile.getClass(), profile.getId());
                therapyComplex.setProfile(profile);
            }
            em.persist(therapyComplex);

            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(TherapyComplex therapyComplex) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            TherapyComplex persistentTherapyComplex = em.find(TherapyComplex.class, therapyComplex.getId());
            Profile profileOld = persistentTherapyComplex.getProfile();
            Profile profileNew = therapyComplex.getProfile();
            if (profileNew != null) {
                profileNew = em.getReference(profileNew.getClass(), profileNew.getId());
                therapyComplex.setProfile(profileNew);
            }
            therapyComplex = em.merge(therapyComplex);

            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = therapyComplex.getId();
                if (findTherapyComplex(id) == null) {
                    throw new NonexistentEntityException("The therapyComplex with id " + id + " no longer exists.");
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
            TherapyComplex therapyComplex;
            try {
                therapyComplex = em.getReference(TherapyComplex.class, id);
                therapyComplex.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The therapyComplex with id " + id + " no longer exists.", enfe);
            }

            em.remove(therapyComplex);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<TherapyComplex> findTherapyComplexEntities() {
        return findTherapyComplexEntities(true, -1, -1);
    }

    public List<TherapyComplex> findTherapyComplexEntities(int maxResults, int firstResult) {
        return findTherapyComplexEntities(false, maxResults, firstResult);
    }

    private List<TherapyComplex> findTherapyComplexEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(TherapyComplex.class));
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

    public TherapyComplex findTherapyComplex(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(TherapyComplex.class, id);
        } finally {
            em.close();
        }
    }

    public int getTherapyComplexCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<TherapyComplex> rt = cq.from(TherapyComplex.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
}
