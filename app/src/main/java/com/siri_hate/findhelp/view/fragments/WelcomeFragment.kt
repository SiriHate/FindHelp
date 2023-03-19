package com.siri_hate.findhelp.view.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.siri_hate.findhelp.R


class WelcomeFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        // Переменная контейнера
        val view = inflater.inflate(R.layout.welcome_fragment, container, false)

        // Обработка нажатия на кнопку "Войти" для смены фрагмента на вход
        view.findViewById<Button>(R.id.Go_login).setOnClickListener {
            val loginFragment = LoginFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.AuthorizationFragment, loginFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        // Обработка нажатия на кнопку "Зарегистрироваться" для смены фрагмента на регистрацию
        view.findViewById<Button>(R.id.Go_register).setOnClickListener {
            val registerFragment = RegisterFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.AuthorizationFragment, registerFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        return view
    }
}



