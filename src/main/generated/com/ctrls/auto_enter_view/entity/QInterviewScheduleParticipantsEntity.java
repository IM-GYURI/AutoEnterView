package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInterviewScheduleParticipantsEntity is a Querydsl query type for InterviewScheduleParticipantsEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewScheduleParticipantsEntity extends EntityPathBase<InterviewScheduleParticipantsEntity> {

    private static final long serialVersionUID = -2127412651L;

    public static final QInterviewScheduleParticipantsEntity interviewScheduleParticipantsEntity = new QInterviewScheduleParticipantsEntity("interviewScheduleParticipantsEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath candidateKey = createString("candidateKey");

    public final StringPath candidateName = createString("candidateName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final DateTimePath<java.time.LocalDateTime> interviewEndDatetime = createDateTime("interviewEndDatetime", java.time.LocalDateTime.class);

    public final StringPath interviewScheduleKey = createString("interviewScheduleKey");

    public final DateTimePath<java.time.LocalDateTime> interviewStartDatetime = createDateTime("interviewStartDatetime", java.time.LocalDateTime.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Long> jobPostingStepId = createNumber("jobPostingStepId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterviewScheduleParticipantsEntity(String variable) {
        super(InterviewScheduleParticipantsEntity.class, forVariable(variable));
    }

    public QInterviewScheduleParticipantsEntity(Path<? extends InterviewScheduleParticipantsEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInterviewScheduleParticipantsEntity(PathMetadata metadata) {
        super(InterviewScheduleParticipantsEntity.class, metadata);
    }

}

