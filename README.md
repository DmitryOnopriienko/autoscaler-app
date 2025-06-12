# Інструкція з використання та деплою

## Налаштування правила для монітору та повідомлення

В GitLab репозиторії `MONITORING` має міститися файл `prometheus/rules.yml`,
в якому описані правила для моніторів. Впевнитись, що в цьому файлі є налаштування
з такою конфігурацією:

```yaml
groups:
  - name: Workers Load Alerts
    rules:
      - alert: CpuUsageHighContinuously
        expr: (sum(openstack_nova_vcpus_used) / sum(openstack_nova_vcpus) * 100) > 50
        for: 10m
        labels:
          severity: warning
        annotations:
          summary: "OpenStack workers have high CPU usage continuously"
          description: "More than 50% of CPU used by OpenStack workers (value={{ $value }})."
```

Це дефолтне налаштування, що перевіряє, чи використання CPU OpenStack workers перевищує 50% протягом 10 хвилин.
Це правило можна змінити відповідно до потреб середовища та додавати інші.

## Підготовка CI Pipeline

В GitLab репозиторії `INFRA/OPENSTACK/OPENSTACK` має бути наявною гілка `autoscaling-ci`,
в якій знаходиться файл `.gitlab-ci.yml` з необхідними шагами для скейлингу. Код пайплайну можна
знайти тут: [.gitlab-ci.yml](.gitlab-ci.yml).
В разі відсутності цієї гілки, її необхідно створити та змінити файл `.gitlab-ci.yml` відповідно до прикладу.


## Деплой

### 1. Створення віртуальної машини
Першим кроком деплою додатку має бути створення віртуальної машини в кластері кафедри,
на якій буде розгорнуто додаток для динамічного масштабування.
Це можуть зробити DevOps інженери кафедри або інші відповідальні особи.

### 2. Створення докер образу додатку
Цей пункт є опціональним, оскільки додаток вже має готовий образ, завантажений на Docker Hub
під тегом `tv12onopriienko/autoscaler-app:1.0-amd`, але в разі необхідності можна зібрати образ наступною командою:

```bash
docker build -t <your_tagging>/autoscaler-app:1.0-amd -f Dockerfile.amd .
```

### 3. Під'єднання до віртуальної машини
Після створення віртуальної машини, необхідно під'єднатись до неї за допомогою SSH.

### 4. Створення файлу конфігурації
В наявній робочій директорії необхідно створити директорію `additional-conf`, в якій створити файл `application.yaml`
з наступним вмістом:

```yaml
gitlab:
  provider: "gitlab.sefl.com.ua"
  project-id: 777
  access-token: "glptt-50111e70123777000000000000"
  default-branch: "autoscaling-ci"
```
Ці дані треба замінити на відповідні вашому GitLab репозиторію.
`provider` - це адреса вашого GitLab сервера, `project-id` - ID проекту,
`access-token` - токен, що дає можливість тригерити GitLab CI пайплайни,
`default-branch` - гілка, на якій знаходиться потрібний CI Pipeline (в нашому випадку - `autoscaling-ci`).

### 5. Запуск додатку
Після створення файлу конфігурації, треба запустити додаток за допомогою Docker. Для цього необхідно перейти до попередньої
робочої директорії, в якій ми створили директорію `additional-conf`, та виконати наступну команду:

```bash
docker run -p 8080:8080 --name autoscaler-app \
 -v $(pwd)/additional-conf:/app/additional-conf \
 tv12onopriienko/autoscaler-app:1.0-amd \
 --spring.config.additional-location="file:./additional-conf/"
```

## Налаштування Alertmanager
В GitLab репозиторії `MONITORING` має міститися файл `alertmanager/alertmanager.yml.tpl`,
в якому описані отримувачі повідомлень про відпрацювання моніторів, та їх налаштування.
Впевнитись, що в цьому файлі є таке налаштування:

```yaml
route:
  receiver: telegram-alerts
  group_by: ['alertname', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 1h

  routes:
    - receiver: 'scaling-webhook'
      match:
        alertname: 'CpuUsageHighContinuously'
receivers:
- name: scaling-webhook
  webhook_configs:
  - url: http://127.0.0.1:8080/alert/prometheus/high-cpu
```

Тут треба замінити IP адресу на ту, за якою доступний додаток для динамічного масштабування (IP адреса віртуальної машини, на якій запущено додаток).

# Додаток готовий до роботи!
