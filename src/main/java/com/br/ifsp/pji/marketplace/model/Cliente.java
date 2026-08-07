package com.br.ifsp.pji.marketplace.model;

import jakarta.persistence.Entity;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;

@Entity
public class Cliente extends Usuario{

	@OneToOne
	@MapsId /*Ver depois o q é*/
	private Usuario usuario;
}
