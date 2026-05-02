import { writable, derived } from 'svelte/store';

const STORAGE_KEY = 'dw_admin_auth';

interface AuthCredentials {
	username: string;
	password: string;
}

function loadAuth(): AuthCredentials | null {
	if (typeof window === 'undefined') return null;
	try {
		const stored = localStorage.getItem(STORAGE_KEY);
		return stored ? JSON.parse(stored) : null;
	} catch {
		return null;
	}
}

function persist(credentials: AuthCredentials | null): void {
	if (typeof window === 'undefined') return;
	try {
		if (credentials) {
			localStorage.setItem(STORAGE_KEY, JSON.stringify(credentials));
		} else {
			localStorage.removeItem(STORAGE_KEY);
		}
	} catch {
		// localStorage may be unavailable
	}
}

function createAuthStore() {
	const { subscribe, set, update } = writable<AuthCredentials | null>(loadAuth());

	function login(username: string, password: string): void {
		const credentials = { username, password };
		persist(credentials);
		set(credentials);
	}

	function logout(): void {
		persist(null);
		set(null);
	}

	return {
		subscribe,
		set,
		update,
		login,
		logout
	};
}

export const auth = createAuthStore();

export const isAuthenticated = derived(auth, ($auth) => $auth !== null);
