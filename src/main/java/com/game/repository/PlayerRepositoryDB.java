package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {
    private final SessionFactory sessionFactory;

    public PlayerRepositoryDB() {
        Properties prop = new Properties();
        prop.put(Environment.DIALECT, "org.hibernate.dialect.MySQL8Dialect");
        prop.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        prop.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");
        prop.put(Environment.USER, "root");
        prop.put(Environment.PASS, "mysql");
        prop.put(Environment.SHOW_SQL, true);
        prop.put(Environment.HBM2DDL_AUTO, "update");
        sessionFactory = new Configuration()
                .addProperties(prop)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        List<Player> allPlayers = new ArrayList<>();
        String nativeSQL = "SELECT * FROM rpg.player LIMIT :pageSize OFFSET :pageNumber";
        try (Session session = sessionFactory.openSession()) {
            NativeQuery<Player> query = session.createNativeQuery(nativeSQL, Player.class);
            query.setParameter("pageNumber", pageNumber * pageSize);
            query.setParameter("pageSize", pageSize);
            allPlayers = query.list();
        }
        return allPlayers;
    }

    @Override
    public int getAllCount() {
        long count = 0L;
        try (Session session = sessionFactory.openSession()) {
            Query<Long> query = session.createNamedQuery("getAllCount", Long.class);
            count = query.getSingleResult();
        }
        return Math.toIntExact(count);
    }

    @Override
    public Player save(Player player) {
        Transaction transaction = null;
        try (Session session = sessionFactory.openSession()) {
            transaction = session.beginTransaction();
            session.save(player);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        Player updatePlayer;
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
//            updatePlayer = (Player) session.merge(player);
            session.saveOrUpdate(player);
            transaction.commit();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        try (Session session = sessionFactory.openSession()) {
            Player playerById = session.find(Player.class, id);
            return Optional.of(playerById);
        }
    }

    @Override
    public void delete(Player player) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.remove(player);
            transaction.commit();
        }
    }

    //    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}