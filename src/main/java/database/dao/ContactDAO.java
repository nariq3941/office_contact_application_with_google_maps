package database.dao;

import database.EntityCRUD;
import database.entity.Address;
import database.entity.Contact;
import exception.DataTooLongViolationException;
import exception.NameUniqueViolationException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.exception.DataException;
import org.hibernate.query.Query;

import java.util.List;

public class ContactDAO implements EntityCRUD<Contact> {
    private SessionFactory sessionFactory;

    public ContactDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public List<Contact> getEntities() {
        List<Contact> contacts;
        try (Session currentSession = sessionFactory.getCurrentSession()) {
            currentSession.beginTransaction();
            Query<Contact> theQuery = currentSession.createQuery("from Contact", Contact.class);
            contacts = theQuery.getResultList();
            currentSession.getTransaction().commit();
        }
        return contacts;
    }

    public void saveEntity(Contact entity) throws DataTooLongViolationException {
        try (Session currentSession = sessionFactory.getCurrentSession()) {
            currentSession.beginTransaction();
            currentSession.saveOrUpdate(entity);
            currentSession.getTransaction().commit();
        } catch (DataException exc) {
            Throwable exceptionCause;
            exceptionCause = new Throwable("przynajmniej jedna z głównych wartości obiektu jest za długa");
            throw new DataTooLongViolationException("Błąd bazy danych", exceptionCause);
        }
    }

    public Contact getEntity(int id) {
        Contact contact;
        try (Session currentSession = sessionFactory.getCurrentSession()) {
            currentSession.beginTransaction();
            contact = currentSession.get(Contact.class, id);
            currentSession.getTransaction().commit();
        }
        return contact;
    }

    public void deleteEntity(int id) {
        try (Session currentSession = sessionFactory.getCurrentSession()) {
            currentSession.beginTransaction();
            currentSession.createQuery("delete from Contact where id=:contactId")
                    .setParameter("contactId", id).executeUpdate();
            currentSession.getTransaction().commit();
        }
    }
}
