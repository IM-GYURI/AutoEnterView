package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMailAlarmInfoEntity is a Querydsl query type for MailAlarmInfoEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMailAlarmInfoEntity extends EntityPathBase<MailAlarmInfoEntity> {

    private static final long serialVersionUID = 50273797L;

    public static final QMailAlarmInfoEntity mailAlarmInfoEntity = new QMailAlarmInfoEntity("mailAlarmInfoEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath interviewScheduleKey = createString("interviewScheduleKey");

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final NumberPath<Long> jobPostingStepId = createNumber("jobPostingStepId", Long.class);

    public final StringPath mailContent = createString("mailContent");

    public final DateTimePath<java.time.LocalDateTime> mailSendDateTime = createDateTime("mailSendDateTime", java.time.LocalDateTime.class);

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QMailAlarmInfoEntity(String variable) {
        super(MailAlarmInfoEntity.class, forVariable(variable));
    }

    public QMailAlarmInfoEntity(Path<? extends MailAlarmInfoEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMailAlarmInfoEntity(PathMetadata metadata) {
        super(MailAlarmInfoEntity.class, metadata);
    }

}

