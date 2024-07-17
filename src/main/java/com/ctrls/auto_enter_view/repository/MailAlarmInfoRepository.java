package com.ctrls.auto_enter_view.repository;

import com.ctrls.auto_enter_view.entity.MailAlarmInfoEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailAlarmInfoRepository extends JpaRepository<MailAlarmInfoEntity, Long> {

  Optional<MailAlarmInfoEntity> findByInterviewScheduleKey(String interviewScheduleKey);
}