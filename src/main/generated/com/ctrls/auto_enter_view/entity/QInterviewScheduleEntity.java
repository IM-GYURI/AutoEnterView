package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QInterviewScheduleEntity is a Querydsl query type for InterviewScheduleEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QInterviewScheduleEntity extends EntityPathBase<InterviewScheduleEntity> {

    private static final long serialVersionUID = -1551986283L;

    public static final QInterviewScheduleEntity interviewScheduleEntity = new QInterviewScheduleEntity("interviewScheduleEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final DatePath<java.time.LocalDate> firstInterviewDate = createDate("firstInterviewDate", java.time.LocalDate.class);

    public final StringPath interviewScheduleKey = createString("interviewScheduleKey");

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Long> jobPostingStepId = createNumber("jobPostingStepId", Long.class);

    public final DatePath<java.time.LocalDate> lastInterviewDate = createDate("lastInterviewDate", java.time.LocalDate.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QInterviewScheduleEntity(String variable) {
        super(InterviewScheduleEntity.class, forVariable(variable));
    }

    public QInterviewScheduleEntity(Path<? extends InterviewScheduleEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QInterviewScheduleEntity(PathMetadata metadata) {
        super(InterviewScheduleEntity.class, metadata);
    }

}

