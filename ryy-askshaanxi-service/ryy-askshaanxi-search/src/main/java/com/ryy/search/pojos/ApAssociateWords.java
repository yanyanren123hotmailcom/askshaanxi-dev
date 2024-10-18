package com.ryy.search.pojos;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Data
@Document("ap_associate_words")
public class ApAssociateWords {
    private static final long serialVersionUID=1L;

    private String id;

    private String associateWords;

    private Date createdTime;

}
