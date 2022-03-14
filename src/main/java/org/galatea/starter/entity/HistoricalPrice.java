package org.galatea.starter.entity;

import java.math.BigDecimal;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HistoricalPrice {

  @Id
  @GeneratedValue(strategy= GenerationType.AUTO)
  private Long historicalPriceId;
  private BigDecimal close;
  private BigDecimal high;
  private BigDecimal low;
  private BigDecimal open;
  private String symbol;
  private long volume;
  private String date;

  @ManyToOne(
      cascade = CascadeType.ALL
  )
  @JoinColumn(referencedColumnName="queryId")
  private Query query;

}