package lesson23_meters;

import scn.Input;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class AdminService {

    LocalDate start = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 1);
    LocalDate end = LocalDate.of(LocalDate.now().getYear(), LocalDate.now().getMonth(), 15);
    int dayTariff = 1000;
    int nightTariff = 700;
    int monthTariff = 1500;

    public ClientAccount find_client(List<ClientAccount> clients, String clientLogin) {
        for (ClientAccount client : clients) {
            if (client.getLogin().equals(clientLogin)) {
                return client;
            }
        }
        return null;
    }
    public Contract find_contract(List<ClientAccount> clients, String clientLogin, String contract_num) {
        ClientAccount currentClient = find_client(clients, clientLogin);
        if (currentClient != null) {
            for (Contract contract : currentClient.getContracts()) {
                if (contract.getContract_num().equals(contract_num)) {
                    return contract;
                }
            }
        }
        return null;
    }
    public void registration(String accounts_path, List<Account> accounts) {
        String login = "";
        String password = "";
        Pattern login_pattern = Pattern.compile("\\w{6,}");
        Pattern pass_pattern = Pattern.compile("(\\w*(?=.*[A-Z])+\\w*){6,}");
        boolean login_ok = false;
        boolean pass_ok = false;
        while (!login_ok && !pass_ok) {
            login = Input.nextLine("Введите логин: ");
            List<String> logins = accounts.stream()
                    .map(Account::getLogin)
                    .toList();
            if (login_pattern.matcher(login).find() && !logins.contains(login)) {
                login_ok = true;
            }
            password = Input.nextLine("Введите пароль: ");
            if (pass_pattern.matcher(password).find()) {
                pass_ok = true;
            }
        }
        if (login_ok && pass_ok) {
            System.out.println("Регистрация успешна!");
            accounts.add(new Account(login, password));
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(accounts_path))) {
                oos.writeObject(accounts);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Логин занят!");
        }
    }

    public void func_1(Account currentAccount, List<ClientAccount> clients) {
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
    public void func_2(List<ClientAccount> clients) {
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
                            ClientAccount currentClient = find_client(clients, clientLogin);
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
                            ClientAccount currentClient = find_client(clients, clientLogin);
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
                                Contract currentContract = find_contract(clients, clientLogin, contract_num);
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
                            ClientAccount currentClient = find_client(clients, clientLogin);
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
                            ClientAccount currentClient = find_client(clients, clientLogin);
                            if (currentClient != null) {
                                Contract contract = find_contract(clients, clientLogin, contract_num);
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

    public void func_3() {
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
