package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeTechStackEntity is a Querydsl query type for ResumeTechStackEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeTechStackEntity extends EntityPathBase<ResumeTechStackEntity> {

    private static final long serialVersionUID = 538520322L;

    public static final QResumeTechStackEntity resumeTechStackEntity = new QResumeTechStackEntity("resumeTechStackEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath resumeKey = createString("resumeKey");

    public final EnumPath<com.ctrls.auto_enter_view.enums.TechStack> techStackName = createEnum("techStackName", com.ctrls.auto_enter_view.enums.TechStack.class);

    public QResumeTechStackEntity(String variable) {
        super(ResumeTechStackEntity.class, forVariable(variable));
    }

    public QResumeTechStackEntity(Path<? extends ResumeTechStackEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeTechStackEntity(PathMetadata metadata) {
        super(ResumeTechStackEntity.class, metadata);
    }

}

