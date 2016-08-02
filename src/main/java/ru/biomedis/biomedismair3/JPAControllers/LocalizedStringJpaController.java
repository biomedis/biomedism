/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
import ru.biomedis.biomedismair3.entity.Language;
import ru.biomedis.biomedismair3.entity.LocalizedString;
import ru.biomedis.biomedismair3.entity.Strings;

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
public class LocalizedStringJpaController implements Serializable {

    public LocalizedStringJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(LocalizedString localizedString)  throws Exception{
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            em.persist(localizedString);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(LocalizedString localizedString) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            localizedString = em.merge(localizedString);
            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = localizedString.getId();
                if (findLocalizedString(id) == null) {
                    throw new NonexistentEntityException("The localizedString with id " + id + " no longer exists.");
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
            LocalizedString localizedString;
            try {
                localizedString = em.getReference(LocalizedString.class, id);
                localizedString.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The localizedString with id " + id + " no longer exists.", enfe);
            }
            em.remove(localizedString);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<LocalizedString> findLocalizedStringEntities() {
        return findLocalizedStringEntities(true, -1, -1);
    }

    public List<LocalizedString> findLocalizedStringEntities(int maxResults, int firstResult) {
        return findLocalizedStringEntities(false, maxResults, firstResult);
    }

    private List<LocalizedString> findLocalizedStringEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(LocalizedString.class));
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

    public LocalizedString findLocalizedString(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(LocalizedString.class, id);
        } finally {
            em.close();
        }
    }

    public int getLocalizedStringCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<LocalizedString> rt = cq.from(LocalizedString.class);
            cq.select(em.getCriteriaBuilder().count(rt));            
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    
    public List<LocalizedString> findByStrings(Strings str)
    {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Select l from LocalizedString l WHERE l.strings = :str");
        query.setParameter("str", str);
        return query.getResultList();
    }
    
    
    public List<LocalizedString> findByStrings(Strings str,Language lang)
    {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Select l from LocalizedString l WHERE l.strings = :str and l.language=:lang");
        query.setParameter("str", str);
        query.setParameter("lang", lang);
        return query.getResultList();
    }
    
    public int countByStrings(Strings str,Language lang)
    {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Select count(l) as cnt from LocalizedString l WHERE l.strings = :str and l.language=:lang");
        query.setParameter("str", str);
        query.setParameter("lang", lang);
        List<Object> results = query.getResultList();
        int count=0;
       for (Object result : results) {
             count = ((Number) result).intValue();
        }
       return count;
    }
    
}
