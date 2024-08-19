package com.ctrls.auto_enter_view.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QJobPostingStepEntity is a Querydsl query type for JobPostingStepEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QJobPostingStepEntity extends EntityPathBase<JobPostingStepEntity> {

    private static final long serialVersionUID = 1894189658L;

    public static final QJobPostingStepEntity jobPostingStepEntity = new QJobPostingStepEntity("jobPostingStepEntity");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath jobPostingKey = createString("jobPostingKey");

    public final StringPath step = createString("step");

    public QJobPostingStepEntity(String variable) {
        super(JobPostingStepEntity.class, forVariable(variable));
    }

    public QJobPostingStepEntity(Path<? extends JobPostingStepEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QJobPostingStepEntity(PathMetadata metadata) {
        super(JobPostingStepEntity.class, metadata);
    }

}

