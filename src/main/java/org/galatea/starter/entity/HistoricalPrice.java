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
  @Column(precision=9, scale=3)
  private BigDecimal close;
  @Column(precision=9, scale=3)
  private BigDecimal high;
  @Column(precision=9, scale=3)
  private BigDecimal low;
  @Column(precision=9, scale=3)
  private BigDecimal open;
  private String symbol;
  private long volume;
  private String date;

}