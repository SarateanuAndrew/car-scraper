package com.example.webscraping.repository;

import com.example.webscraping.model.dbo.CarDbo;
import com.example.webscraping.model.enums.Source;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CarRepository extends JpaRepository<CarDbo, Long> {
    @Query("SELECT c " +
            "FROM CarDbo c " +
            "WHERE c.price BETWEEN :priceMin AND :priceMax AND " +
            "c.millage BETWEEN :millageMin AND :millageMax AND " +
            "c.displacement BETWEEN :displacementMin AND :displacementMax AND " +
            "c.source IN :sourceList AND c.title LIKE :title")
    Page<CarDbo> findCarByTitle(@Param("title") String title, Pageable pageable,
                                @Param("priceMin") Integer priceMin, @Param("priceMax") Integer priceMax,
                                @Param("millageMin") Integer millageMin, @Param("millageMax") Integer millageMax,
                                @Param("displacementMin") Double displacementMin, @Param("displacementMax") Double displacementMax,
                                @Param("sourceList") List<Source> sourceList);
}
