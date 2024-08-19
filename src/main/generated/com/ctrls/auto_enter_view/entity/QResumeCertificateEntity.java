package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QResumeCertificateEntity is a Querydsl query type for ResumeCertificateEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QResumeCertificateEntity extends EntityPathBase<ResumeCertificateEntity> {

    private static final long serialVersionUID = -741748729L;

    public static final QResumeCertificateEntity resumeCertificateEntity = new QResumeCertificateEntity("resumeCertificateEntity");

    public final DatePath<java.time.LocalDate> certificateDate = createDate("certificateDate", java.time.LocalDate.class);

    public final StringPath certificateName = createString("certificateName");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath resumeKey = createString("resumeKey");

    public QResumeCertificateEntity(String variable) {
        super(ResumeCertificateEntity.class, forVariable(variable));
    }

    public QResumeCertificateEntity(Path<? extends ResumeCertificateEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QResumeCertificateEntity(PathMetadata metadata) {
        super(ResumeCertificateEntity.class, metadata);
    }

}

