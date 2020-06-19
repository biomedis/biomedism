/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.Complex;
import ru.biomedis.biomedismair3.entity.Section;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityNotFoundException;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author Anama
 */
public class ComplexJpaController implements Serializable {

    public ComplexJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Complex complex)  throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(complex);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Complex complex) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            complex = em.merge(complex);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = complex.getId();
                if (findComplex(id) == null) {
                    throw new NonexistentEntityException("The complex with id " + id + " no longer exists.");
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
            Complex complex;
            try {
                complex = em.getReference(Complex.class, id);
                complex.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The complex with id " + id + " no longer exists.", enfe);
            }
            em.remove(complex);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Complex> findComplexEntities() {
        return findComplexEntities(true, -1, -1);
    }

    public List<Complex> findComplexEntities(int maxResults, int firstResult) {
        return findComplexEntities(false, maxResults, firstResult);
    }

    private List<Complex> findComplexEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {

            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery cq = cb.createQuery();
            Root<Complex> c = cq.from(Complex.class);
            cq.select(cq.from(Complex.class));
            cq.orderBy(cb.asc(c.get("id")));

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

    public Complex findComplex(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Complex.class, id);
        } finally {
            em.close();
        }
    }

    public int getComplexCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Complex> rt = cq.from(Complex.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
      public List<Complex> findAllComplexBySection(Section section)
    {
          EntityManager em = getEntityManager();
          Query query = em.createQuery("SELECT p FROM Complex p WHERE p.section=:section order by p.id asc");
          query.setParameter("section", section);
          return query.getResultList();
    }
}
