package dev.java.db.daos;

import dev.java.db.model.CandidateFeedback;
import org.hibernate.SessionFactory;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;

public class CandidateFeedbackDao extends AbstractDao<CandidateFeedback> {
    public CandidateFeedbackDao(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    @Override
    public List<CandidateFeedback> getSortedEntitiesPage(int pageNumber, String sortedField,
                                                         boolean order, int itemsNumberInPage) {
        CriteriaBuilder criteriaBuilder = getSessionFactory().getCriteriaBuilder();
        CriteriaQuery<CandidateFeedback> query = criteriaBuilder.createQuery(CandidateFeedback.class);
        Root<CandidateFeedback> root = query.from(CandidateFeedback.class);

        if (order) {
            query = query.select(root).orderBy(criteriaBuilder.asc(root.get(sortedField)));
        } else {
            query = query.select(root).orderBy(criteriaBuilder.desc(root.get(sortedField)));
        }

        TypedQuery<CandidateFeedback> typedQuery = getSessionFactory().getCurrentSession().createQuery(query);
        typedQuery.setFirstResult((pageNumber - 1) * itemsNumberInPage);
        typedQuery.setMaxResults(itemsNumberInPage);

        return typedQuery.getResultList();
    }

    @Override
    public List<CandidateFeedback> getFilteredEntitiesPage(String... params) {
        return null;
    }

    @Override
    public CandidateFeedback getEntityById(long id) {
        return getSessionFactory().getCurrentSession().get(CandidateFeedback.class, id);
    }
}