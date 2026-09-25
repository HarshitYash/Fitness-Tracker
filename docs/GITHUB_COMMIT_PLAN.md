# GitHub commit plan — commit after every completed functionality

Use small, meaningful commits. Do not wait until the whole project is finished.

1. `chore: initialize spring boot fitness project`
2. `feat: add fitness domain entities`
3. `feat: map user activity recommendation relationships`
4. `feat: add repositories`
5. `feat: add user registration service`
6. `feat: add registration endpoint and validation`
7. `feat: add activity create and list APIs`
8. `feat: add recommendation APIs`
9. `feat: add password hashing`
10. `feat: add spring security configuration`
11. `feat: add jwt authentication`
12. `feat: add role based authorization`
13. `feat: add global exception handling`
14. `feat: add swagger api documentation`
15. `feat: add docker support`
16. `docs: add local setup and api examples`
17. `chore: prepare deployment configuration`

Basic workflow after each functionality:
```bash
git status
git add .
git commit -m "feat: describe the completed functionality"
git push origin main
```

Before the first push:
```bash
git init
git branch -M main
git remote add origin <YOUR_GITHUB_REPOSITORY_URL>
git add .
git commit -m "chore: initialize spring boot fitness project"
git push -u origin main
```
