package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeImageEntity is a Querydsl query type for ResumeImageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeImageEntity extends EntityPathBase<ResumeImageEntity> {

    private static final long serialVersionUID = 1846205067L;

    public static final QResumeImageEntity resumeImageEntity = new QResumeImageEntity("resumeImageEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath resumeImageUrl = createString("resumeImageUrl");

    public final StringPath resumeKey = createString("resumeKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QResumeImageEntity(String variable) {
        super(ResumeImageEntity.class, forVariable(variable));
    }

    public QResumeImageEntity(Path<? extends ResumeImageEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeImageEntity(PathMetadata metadata) {
        super(ResumeImageEntity.class, metadata);
    }

}

