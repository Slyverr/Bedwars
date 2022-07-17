package com.slyvr.exception;

@SuppressWarnings("serial")
public class ArenaNotReadyException extends RuntimeException {

	public ArenaNotReadyException(String msg) {
		super(msg);
	}

}