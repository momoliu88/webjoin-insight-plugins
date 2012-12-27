package com.ebupt.webjoin.insight.intercept.util;

import java.io.Serializable;

public class KeyValPair<K, V> implements Serializable {
	private static final long serialVersionUID = -2699455900190542636L;
	private final K key;
	private final V value;

	public KeyValPair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	public K getKey() {
		return this.key;
	}

	public V getValue() {
		return this.value;
	}

	public int hashCode() {
		int result = 1;
		int prime = 31;
		result = prime * result + this.key.hashCode();
		result = prime * result + this.value.hashCode();
		return result;
	}

	@SuppressWarnings("unchecked")
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		KeyValPair<K,V> other = (KeyValPair<K, V>) obj;
		return (other.key.equals(this.key)) && (other.value.equals(this.value));
	}

	public String toString() {
		return this.key + "=" + this.value;
	}
}