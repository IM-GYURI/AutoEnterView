package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeCareerEntity is a Querydsl query type for ResumeCareerEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeCareerEntity extends EntityPathBase<ResumeCareerEntity> {

    private static final long serialVersionUID = 1089985844L;

    public static final QResumeCareerEntity resumeCareerEntity = new QResumeCareerEntity("resumeCareerEntity");

    public final NumberPath<Integer> calculatedCareer = createNumber("calculatedCareer", Integer.class);

    public final StringPath companyName = createString("companyName");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final EnumPath<com.ctrls.auto_enter_view.enums.JobCategory> jobCategory = createEnum("jobCategory", com.ctrls.auto_enter_view.enums.JobCategory.class);

    public final StringPath resumeKey = createString("resumeKey");

    public final DatePath<java.time.LocalDate> startDate = createDate("startDate", java.time.LocalDate.class);

    public QResumeCareerEntity(String variable) {
        super(ResumeCareerEntity.class, forVariable(variable));
    }

    public QResumeCareerEntity(Path<? extends ResumeCareerEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeCareerEntity(PathMetadata metadata) {
        super(ResumeCareerEntity.class, metadata);
    }

}

