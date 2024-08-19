package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QAppliedJobPostingEntity is a Querydsl query type for AppliedJobPostingEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QAppliedJobPostingEntity extends EntityPathBase<AppliedJobPostingEntity> {

    private static final long serialVersionUID = 2052919295L;

    public static final QAppliedJobPostingEntity appliedJobPostingEntity = new QAppliedJobPostingEntity("appliedJobPostingEntity");

    public final DatePath<java.time.LocalDate> appliedDate = createDate("appliedDate", java.time.LocalDate.class);

    public final StringPath candidateKey = createString("candidateKey");

    public final DatePath<java.time.LocalDate> endDate = createDate("endDate", java.time.LocalDate.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final StringPath stepName = createString("stepName");

    public final StringPath title = createString("title");

    public QAppliedJobPostingEntity(String variable) {
        super(AppliedJobPostingEntity.class, forVariable(variable));
    }

    public QAppliedJobPostingEntity(Path<? extends AppliedJobPostingEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QAppliedJobPostingEntity(PathMetadata metadata) {
        super(AppliedJobPostingEntity.class, metadata);
    }

}

