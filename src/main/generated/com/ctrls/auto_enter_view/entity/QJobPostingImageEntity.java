package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobPostingImageEntity is a Querydsl query type for JobPostingImageEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPostingImageEntity extends EntityPathBase<JobPostingImageEntity> {

    private static final long serialVersionUID = -1953636877L;

    public static final QJobPostingImageEntity jobPostingImageEntity = new QJobPostingImageEntity("jobPostingImageEntity");

    public final QBaseEntity _super = new QBaseEntity(this);

    public final StringPath companyImageUrl = createString("companyImageUrl");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> createdAt = _super.createdAt;

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    //inherited
    public final DateTimePath<java.time.LocalDateTime> updatedAt = _super.updatedAt;

    public QJobPostingImageEntity(String variable) {
        super(JobPostingImageEntity.class, forVariable(variable));
    }

    public QJobPostingImageEntity(Path<? extends JobPostingImageEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobPostingImageEntity(PathMetadata metadata) {
        super(JobPostingImageEntity.class, metadata);
    }

}

