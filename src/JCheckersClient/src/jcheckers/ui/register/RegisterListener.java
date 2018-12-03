package jcheckers.ui.register;

public interface RegisterListener {

	void onCancel();

	void onRegister(String username, char[] password, String email);

}
