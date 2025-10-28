package com.iseeyou.fortunetelling.repository.user;

import com.iseeyou.fortunetelling.entity.user.SeerSpeciality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SeerSpecialityRepository extends JpaRepository<SeerSpeciality, String> {
}

