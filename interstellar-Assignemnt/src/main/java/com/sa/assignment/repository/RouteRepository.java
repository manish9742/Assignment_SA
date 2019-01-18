package com.sa.assignment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import com.sa.assignment.orm.Route;

@RepositoryRestResource
public interface RouteRepository extends JpaRepository<Route, Short>
{

}
