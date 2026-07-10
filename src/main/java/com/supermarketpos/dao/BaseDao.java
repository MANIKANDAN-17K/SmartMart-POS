package com.supermarketpos.dao;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract shared by every DAO in the project.
 * "delete" is semantic, not literal — implementations that store soft-deletable
 * entities (like User) should perform a soft delete rather than a row removal.
 */
public interface BaseDao<T, ID> {

    Optional<T> findById(ID id);

    List<T> findAll();

    T save(T entity);

    T update(T entity);

    void delete(ID id);
}
