package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeEntity is a Querydsl query type for ResumeEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeEntity extends EntityPathBase<ResumeEntity> {

    private static final long serialVersionUID = 879813206L;

    public static final QResumeEntity resumeEntity = new QResumeEntity("resumeEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath address = createString("address");

    public final DatePath<java.time.LocalDate> birthDate = createDate("birthDate", java.time.LocalDate.class);

    public final StringPath candidateKey = createString("candidateKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final EnumPath<com.ctrls.auto_enter_view.enums.Education> education = createEnum("education", com.ctrls.auto_enter_view.enums.Education.class);

    public final StringPath email = createString("email");

    public final StringPath gender = createString("gender");

    public final EnumPath<com.ctrls.auto_enter_view.enums.JobCategory> jobWant = createEnum("jobWant", com.ctrls.auto_enter_view.enums.JobCategory.class);

    public final StringPath name = createString("name");

    public final StringPath phoneNumber = createString("phoneNumber");

    public final StringPath portfolio = createString("portfolio");

    public final StringPath resumeKey = createString("resumeKey");

    public final StringPath schoolName = createString("schoolName");

    public final StringPath title = createString("title");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QResumeEntity(String variable) {
        super(ResumeEntity.class, forVariable(variable));
    }

    public QResumeEntity(Path<? extends ResumeEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeEntity(PathMetadata metadata) {
        super(ResumeEntity.class, metadata);
    }

}

