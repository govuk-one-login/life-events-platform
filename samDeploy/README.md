# Working with SAM

## Setup
To set up sam and be able to run it in the correct environment, run the following commands:

Run these
```shell
brew install gradle@7
echo '# Add gradle 7 to PATH' >> ~/.zshrc
echo 'export PATH="/opt/homebrew/opt/gradle@7/bin:$PATH"' >> ~/.zshrc
echo '' >> ~/.zshrc

brew install jenv
echo '# Add jenv to PATH and initialise' >> ~/.zshrc
echo 'export PATH="$HOME/.jenv/bin:$PATH"' >> ~/.zshrc
echo 'eval "$(jenv init -)"' >> ~/.zshrc
echo '' >> ~/.zshrc

brew install aws-sam-cli

brew tap homebrew/cask-versions
brew install corretto17
```
Restart terminal then run
```shell
jenv add /Users/${USER}/Library/Java/JavaVirtualMachines/corretto-17.0.7/Contents/Home/
jenv global corretto64-17.0.7
```
Restart terminal then run in the project top directory
```shell
sam build -t samDeploy/template.yaml
```
