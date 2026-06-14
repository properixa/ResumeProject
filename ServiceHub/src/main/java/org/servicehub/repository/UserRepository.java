package org.servicehub.repository;

import org.servicehub.entity.ServicehubUser;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<ServicehubUser, Long> {
}
