package io.mosip.mock.authdata.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import io.mosip.mock.authdata.entity.MockIdentity;

@Repository
public interface MockIdentityRepository extends CrudRepository<MockIdentity, String>{

}
