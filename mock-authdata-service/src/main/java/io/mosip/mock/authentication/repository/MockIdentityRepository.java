package io.mosip.mock.authentication.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.mosip.mock.authentication.entity.MockIdentity;

@Repository
public interface MockIdentityRepository extends CrudRepository<MockIdentity, String>{

}
