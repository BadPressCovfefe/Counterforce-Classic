/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storage;

/**
 *
 * @author tobster
 */
public interface GameLoadSaveListener
{
    void LoadError(String strDescription);
    void LoadWarning(String strDescription);
    void SaveError(String strDescription);
    void StartAPI();
}
