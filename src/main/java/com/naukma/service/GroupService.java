package com.naukma.service;

import com.naukma.dao.GroupDAO;
import com.naukma.model.ProductGroup;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GroupService {

    private final GroupDAO dao;

    public GroupService(GroupDAO dao) {
        this.dao = dao;
    }

    public void create(ProductGroup group) throws SQLException {
        dao.insert(group);
    }

    public Optional<ProductGroup> findById(String id) throws SQLException {
        return dao.findById(id);
    }

    public List<ProductGroup> findAll() throws SQLException {
        return dao.findAll();
    }

    public void update(ProductGroup group) throws SQLException {
        dao.update(group);
    }

    public void delete(String id) throws SQLException {
        dao.delete(id);
    }

    public void addMember(String groupId, String productId) throws SQLException {
        dao.addMember(groupId, productId);
    }

    public void removeMember(String groupId, String productId) throws SQLException {
        dao.removeMember(groupId, productId);
    }
}