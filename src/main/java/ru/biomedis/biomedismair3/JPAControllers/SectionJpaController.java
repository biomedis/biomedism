/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.biomedis.biomedismair3.JPAControllers;

import ru.biomedis.biomedismair3.JPAControllers.exceptions.NonexistentEntityException;
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
public class SectionJpaController implements Serializable {

    public SectionJpaController(EntityManagerFactory emf) {
        this.emf = emf;
    }
    private EntityManagerFactory emf = null;

    public EntityManager getEntityManager() {
        return emf.createEntityManager();
    }

    public void create(Section section) throws Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
            Section parent = section.getParent();
            if (parent != null) {
                parent = em.getReference(parent.getClass(), parent.getId());
                section.setParent(parent);
            }
            em.persist(section);
            /*
            циклически ставит парента это не надо
            if (parent != null) {
                Section oldParentOfParent = parent.getParent();
                if (oldParentOfParent != null) {
                    oldParentOfParent.setParent(null);
                    oldParentOfParent = em.merge(oldParentOfParent);
                }
                parent.setParent(section);
                parent = em.merge(parent);


            }
            */
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public void edit(Section section) throws NonexistentEntityException, Exception {
        EntityManager em = null;
        try {
            em = getEntityManager();
            em.getTransaction().begin();
           // Section persistentSection = em.find(Section.class, section.getId());
            //Section parentOld = persistentSection.getParent();
            Section parentNew = section.getParent();
            if (parentNew != null) {
                parentNew = em.getReference(parentNew.getClass(), parentNew.getId());
                section.setParent(parentNew);
            }
            section = em.merge(section);


            /*
            if (parentOld != null && !parentOld.equals(parentNew)) {
                parentOld.setParent(null);
                parentOld = em.merge(parentOld);
            }
            if (parentNew != null && !parentNew.equals(parentOld)) {
                Section oldParentOfParent = parentNew.getParent();
                if (oldParentOfParent != null) {
                    oldParentOfParent.setParent(null);
                    oldParentOfParent = em.merge(oldParentOfParent);
                }
                parentNew.setParent(section);
                parentNew = em.merge(parentNew);
            }
            */


            em.getTransaction().commit();
        } catch (Exception ex) {
            String msg = ex.getLocalizedMessage();
            if (msg == null || msg.length() == 0) {
                Long id = section.getId();
                if (findSection(id) == null) {
                    throw new NonexistentEntityException("The section with id " + id + " no longer exists.");
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
            Section section;
            try {
                section = em.getReference(Section.class, id);
                section.getId();
            } catch (EntityNotFoundException enfe) {
                throw new NonexistentEntityException("The section with id " + id + " no longer exists.", enfe);
            }
            /*
            Section parent = section.getParent();
            if (parent != null) {
                parent.setParent(null);
                parent = em.merge(parent);
            }
            */
            em.remove(section);
            em.getTransaction().commit();
        } finally {
            if (em != null) {
                em.close();
            }
        }
    }

    public List<Section> findSectionEntities() {
        return findSectionEntities(true, -1, -1);
    }

    public List<Section> findSectionEntities(int maxResults, int firstResult) {
        return findSectionEntities(false, maxResults, firstResult);
    }

    private List<Section> findSectionEntities(boolean all, int maxResults, int firstResult) {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            cq.select(cq.from(Section.class));
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

    public Section findSection(Long id) {
        EntityManager em = getEntityManager();
        try {
            return em.find(Section.class, id);
        } finally {
            em.close();
        }
    }

    public int getSectionCount() {
        EntityManager em = getEntityManager();
        try {
            CriteriaQuery cq = em.getCriteriaBuilder().createQuery();
            Root<Section> rt = cq.from(Section.class);
            cq.select(em.getCriteriaBuilder().count(rt));
            Query q = em.createQuery(cq);
            return ((Long) q.getSingleResult()).intValue();
        } finally {
            em.close();
        }
    }
    
    
    
}
