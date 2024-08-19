package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCandidateEntity is a Querydsl query type for CandidateEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCandidateEntity extends EntityPathBase<CandidateEntity> {

    private static final long serialVersionUID = -1385478752L;

    public static final QCandidateEntity candidateEntity = new QCandidateEntity("candidateEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath candidateKey = createString("candidateKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final StringPath email = createString("email");

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final EnumPath<com.ctrls.auto_enter_view.enums.UserRole> role = createEnum("role", com.ctrls.auto_enter_view.enums.UserRole.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QCandidateEntity(String variable) {
        super(CandidateEntity.class, forVariable(variable));
    }

    public QCandidateEntity(Path<? extends CandidateEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCandidateEntity(PathMetadata metadata) {
        super(CandidateEntity.class, metadata);
    }

}

