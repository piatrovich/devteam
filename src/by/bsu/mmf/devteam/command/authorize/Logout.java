package by.bsu.mmf.devteam.command.authorize;

import by.bsu.mmf.devteam.command.Command;
import by.bsu.mmf.devteam.exception.infrastructure.CommandException;
import by.bsu.mmf.devteam.resource.ResourceManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class Logout extends Command {
    private static final String LOGIN_PAGE = "forward.common.login";

    @Override
    public void execute(HttpServletRequest request, HttpServletResponse response) throws CommandException {
        request.getSession().invalidate();
        setForward(ResourceManager.getProperty(LOGIN_PAGE));
    }

}