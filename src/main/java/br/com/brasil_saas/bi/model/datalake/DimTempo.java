package br.com.brasil_saas.bi.model.datalake;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dim_tempo", schema = "brasil_saas_dl")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class DimTempo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "data", nullable = false, unique = true)
    private LocalDate data;

    @Column(name = "dia_da_semana", nullable = false)
    private Integer diaDaSemana; // 1=Domingo, 2=Segunda, ..., 7=Sábado

    @Column(name = "nome_dia_da_semana", nullable = false, length = 20)
    private String nomeDiaDaSemana;

    @Column(name = "dia_do_mes", nullable = false)
    private Integer diaDoMes;

    @Column(name = "dia_do_ano", nullable = false)
    private Integer diaDoAno;

    @Column(name = "semana_do_ano", nullable = false)
    private Integer semanaDoAno;

    @Column(name = "mes", nullable = false)
    private Integer mes;

    @Column(name = "nome_mes", nullable = false, length = 20)
    private String nomeMes;

    @Column(name = "trimestre", nullable = false)
    private Integer trimestre;

    @Column(name = "ano", nullable = false)
    private Integer ano;

    @Column(name = "eh_fim_de_semana", nullable = false)
    private Boolean ehFimDeSemana;

    @Column(name = "eh_feriado", nullable = false)
    private Boolean ehFeriado = false;

    @Column(name = "nome_feriado", length = 100)
    private String nomeFeriado;

    @Column(name = "data_criacao")
    private LocalDate dataCriacao = LocalDate.now();
}
