package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeExperienceEntity is a Querydsl query type for ResumeExperienceEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeExperienceEntity extends EntityPathBase<ResumeExperienceEntity> {

    private static final long serialVersionUID = -742343520L;

    public static final QResumeExperienceEntity resumeExperienceEntity = new QResumeExperienceEntity("resumeExperienceEntity");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final StringPath experienceName = createString("experienceName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath resumeKey = createString("resumeKey");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public QResumeExperienceEntity(String variable) {
        super(ResumeExperienceEntity.class, forVariable(variable));
    }

    public QResumeExperienceEntity(Path<? extends ResumeExperienceEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeExperienceEntity(PathMetadata metadata) {
        super(ResumeExperienceEntity.class, metadata);
    }

}

