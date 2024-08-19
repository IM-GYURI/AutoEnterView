package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCandidateListEntity is a Querydsl query type for CandidateListEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCandidateListEntity extends EntityPathBase<CandidateListEntity> {

    private static final long serialVersionUID = 723930462L;

    public static final QCandidateListEntity candidateListEntity = new QCandidateListEntity("candidateListEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath candidateKey = createString("candidateKey");

    public final StringPath candidateListKey = createString("candidateListKey");

    public final StringPath candidateName = createString("candidateName");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Long> jobPostingStepId = createNumber("jobPostingStepId", Long.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCandidateListEntity(String variable) {
        super(CandidateListEntity.class, forVariable(variable));
    }

    public QCandidateListEntity(Path<? extends CandidateListEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCandidateListEntity(PathMetadata metadata) {
        super(CandidateListEntity.class, metadata);
    }

}

