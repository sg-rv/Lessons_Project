package lesson23_meters;

import scn.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Admin {
    public Admin() {
        String accounts_path = "D:\\SE\\JavaProjects\\Lessons_Project\\" +
                "src\\main\\java\\lesson23_meters\\admin_accounts.csv";
        List<Account> accounts = deserialize(accounts_path);
        AdminService adminService = new AdminService();
        boolean logged_in = false;
        String inp = "";
        Account currentAccount = null;
        while (!inp.equals("3")) {
            inp = Input.nextLine("""
                    1. Регистрация
                    2. Вход в личный кабинет
                    3. Выход
                    """);
            if (inp.equals("1")) {
                adminService.registration(accounts_path, accounts);
            } else if (inp.equals("2")) {
                String login = Input.nextLine("Введите логин: ");
                String password = Input.nextLine("Введите пароль: ");
                for (Account account : accounts) {
                    if(account.getPass().equals(password) && account.getLogin().equals(login)) {
                        currentAccount = account;
                        logged_in = true;
                        break;
                    }
                }
                if (logged_in) {
                    inp = "3";
                } else {
                    System.out.println("Неверный логин или пароль! ");
                }
            }
        }
        if (logged_in) {
            String objects_path =
                    "D:\\SE\\JavaProjects\\Lessons_Project\\" +
                            "src\\main\\java\\lesson23_meters\\clients_objects";
            String client_objects_path = objects_path
                    + File.separator
                    + currentAccount.getLogin()
                    + "_clients.csv";
            List<ClientAccount> clients = deserialize(client_objects_path);
            if(clients.isEmpty()) {
                System.out.println("Нет клиентов!");
            }
            String func = "";
            while(!func.equals("4")) {
                func = Input.nextLine("""
                        1.Выгрузить всех пользователей в формате csv
                        2.Работа с клиентами
                        3.Задать интервал даты для передачи показаний
                        4.Выход
                        """);
                switch (func) {
                    case "1" -> {
                       adminService.func_1(currentAccount, clients);
                    }
                    case "2" -> {
                        adminService.func_2(clients);
                    }
                    case "3" -> {
                        adminService.func_3();
                    }
                }
            }
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(client_objects_path))) {
                oos.writeObject(clients);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    static <T> List<T> deserialize(String filePath) {
        File accounts_file = new File(filePath);
        if (accounts_file.exists()) {
            try (ObjectInputStream oos = new ObjectInputStream(
                    new FileInputStream(filePath))) {
                return (List<T>) oos.readObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return new ArrayList<>();
    }
    public static void main(String[] args) {
        new Admin();
    }
}