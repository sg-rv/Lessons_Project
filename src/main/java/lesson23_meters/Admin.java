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
        ClientService clientService = new ClientService();
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
                clientService.registration(accounts_path,accounts);
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
            LocalDate start = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
            LocalDate end = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 15);
            int dayTariff = 1000;
            int nightTariff = 700;
            int monthTariff = 1500;
            while(!func.equals("4")) {
                func = Input.nextLine("""
                        1.Выгрузить всех пользователей в формате csv
                        2.Работа с клиентами
                        3.Задать интервал даты для передачи показаний
                        4.Выход
                        """);
                for (ClientAccount client : clients) {
                    for (Contract contract : client.getContracts()) {
                        for (Meter meter : contract.getMeters()) {
                            System.out.println(meter.getMeter_num());
                        }
                    }
                }
                switch (func) {
                    case "1" -> {
                        String contracts_docs_path =
                                "D:\\SE\\JavaProjects\\Lessons_Project\\" +
                                        "src\\main\\java\\lesson23_meters\\contracts";
                        String contracts_table_path = contracts_docs_path
                                + File.separator
                                + currentAccount.getLogin()
                                + "_contracts.csv";
                        List<String> allContracts = new ArrayList<>();
                        for (ClientAccount client : clients) {
                            StringBuilder client_contracts_str = new StringBuilder(client.getLogin() + ";"
                                    + client.getName());
                            for (Contract contract : client.getContracts()) {
                                client_contracts_str.append(";").append(contract.getContract_num());
                            }
                            allContracts.add(client_contracts_str.toString());
                        }
                        try (BufferedWriter bw = new BufferedWriter(new FileWriter(contracts_table_path))) {
                            for (String allContract : allContracts) {
                                bw.write(allContract + "\n");
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case "2" -> {
                        System.out.println("Ожидание...");
                        int port = 3498;
                        try (ServerSocket serverSocket = new ServerSocket(port)) {
                            String exit = "";
                            while (!exit.equals("да")) {
                                Socket accept = serverSocket.accept();
                                System.out.println("Связь установлена...");
                                BufferedReader reader = new BufferedReader(new InputStreamReader(
                                        accept.getInputStream()));
                                ObjectInputStream ois = new ObjectInputStream(
                                        accept.getInputStream());
                                PrintStream writer = new PrintStream(
                                        accept.getOutputStream(), true);
                                String request = reader.readLine();
                                System.out.println("Запрос получен...");
                                String acceptance = Input.nextLine("Принять? ");
                                if (acceptance.equals("да")) {
                                    String[] split = request.split(";");
                                    String service = split[0];
                                    String clientName = split[1];
                                    String clientLogin = split[2];
                                    switch (service) {
                                        case "1" -> {
                                            Contract new_contract = (Contract) ois.readObject();
                                            ClientAccount currentClient = clientService.find_client(clients, clientLogin);
                                            if (currentClient != null) {
                                                currentClient.getContracts().add(new_contract);
                                            } else {
                                                ClientAccount temp_client = new ClientAccount(clientLogin, "",
                                                        clientName);
                                                temp_client.getContracts().add(new_contract);
                                                clients.add(temp_client);
                                            }
                                            writer.println("OK");
                                            writer.println("Ваш запрос принят");
                                            System.out.println("Новый договор подписан!");
                                        }
                                        case "2" -> {
                                            String contract_num = reader.readLine();
                                            Meter new_meter = (Meter) ois.readObject();
                                            ClientAccount currentClient = clientService.find_client(clients, clientLogin);
                                            if (currentClient != null) {
                                                for (Contract contract : currentClient.getContracts()) {
                                                    if (contract.getContract_num().equals(contract_num)) {
                                                        contract.getMeters().add(new_meter);
                                                        writer.println("OK");
                                                        writer.println("Новый счетчик добавлен!");
                                                        break;
                                                    }
                                                }
                                            } else {
                                                writer.println("NOT OK");
                                                writer.println("Данные не совпадают!");
                                            }
                                        }
                                        case "3" -> {
                                            if (LocalDate.now().isAfter(start) && LocalDate.now().isBefore(end)) {
                                                String contract_num = reader.readLine();
                                                List<Meter> sent_meters = (List<Meter>) ois.readObject();
                                                Contract currentContract = clientService
                                                        .find_contract(clients, clientLogin, contract_num);
                                                if (currentContract != null) {
                                                    List<Meter> meters_of_this_contract = currentContract.getMeters();
                                                    int sum = 0;
                                                    for (int i = 0; i < meters_of_this_contract.size(); i++) {
                                                        Meter current_meter = meters_of_this_contract.get(i);
                                                        Meter sent_meter = sent_meters.get(i);
                                                        if (current_meter.getMonth_hour().equals("Month")) {
                                                            current_meter.setThis_month(sent_meter.getThis_month());
                                                            sum += current_meter.calculateByMonths() * monthTariff;
                                                        } else if (current_meter.getMonth_hour().equals("Hour")) {
                                                            current_meter.setTotal_days(sent_meter.getTotal_days());
                                                            current_meter.setTotal_nights(sent_meter.getTotal_nights());
                                                            sum += current_meter.calculateByHours(dayTariff,nightTariff);
                                                        }
                                                    }
                                                    writer.println("OK");
                                                    writer.println("Итого к оплате: " + sum + " руб.");
                                                } else {
                                                    writer.println("NOT OK");
                                                    writer.println("Данные не совпадают!");
                                                }
                                            } else {
                                                writer.println("NOT OK");
                                                writer.println("Данные переданы не в сроки");
                                            }
                                        }
                                        case "4" -> {
                                            Contract contract_to_delete = (Contract) ois.readObject();
                                            ClientAccount currentClient = clientService.find_client(clients, clientLogin);
                                            if (currentClient == null) {
                                                writer.println("NOT OK");
                                                writer.println("Произошла ошибка");
                                            } else {
                                                currentClient.getContracts().remove(contract_to_delete);
                                                writer.println("OK");
                                                writer.println("Договор аннулирован");
                                            }
                                        }
                                        case "5" -> {
                                            String contract_num = reader.readLine();
                                            Meter meter_to_delete = (Meter) ois.readObject();
                                            ClientAccount currentClient = clientService.find_client(clients, clientLogin);
                                            if (currentClient != null) {
                                                Contract contract = clientService
                                                        .find_contract(clients, clientLogin, contract_num);
                                                assert contract != null;
                                                if (contract.getMeters().remove(meter_to_delete)) {
                                                    writer.println("OK");
                                                    writer.println("Счетчик удален!");
                                                }
                                            } else {
                                                writer.println("NOT OK");
                                                writer.println("Данные не совпадают!");
                                            }
                                        }
                                    }
                                }
                                ois.close();
                                writer.close();
                                reader.close();
                                exit = Input.nextLine("Выйти? ");
                            }
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                    }
                    case "3" -> {
                        int new_start_date = Input.nextInt("Введите новый день начала: ");
                        int new_end_date = Input.nextInt("Введите новый последний день: ");
                        start = LocalDate.of(LocalDate.now().getYear(),
                                LocalDate.now().getMonth(),
                                new_start_date);
                        end = LocalDate.of(LocalDate.now().getYear(),
                                LocalDate.now().getMonth(),
                                new_end_date);
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