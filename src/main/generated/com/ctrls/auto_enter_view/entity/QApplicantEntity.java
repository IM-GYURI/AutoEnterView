package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QApplicantEntity is a Querydsl query type for ApplicantEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QApplicantEntity extends EntityPathBase<ApplicantEntity> {

    private static final long serialVersionUID = 104035295L;

    public static final QApplicantEntity applicantEntity = new QApplicantEntity("applicantEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath candidateKey = createString("candidateKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Integer> score = createNumber("score", Integer.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QApplicantEntity(String variable) {
        super(ApplicantEntity.class, forVariable(variable));
    }

    public QApplicantEntity(Path<? extends ApplicantEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QApplicantEntity(PathMetadata metadata) {
        super(ApplicantEntity.class, metadata);
    }

}

